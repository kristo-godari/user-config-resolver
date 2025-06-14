package resolver

import (
	"encoding/json"
	"fmt"
	"go/ast"
	"go/parser"
	"go/token"
	"regexp"
	"strings"

	"configresolver/resolver/dto"
)

// JsonConfigResolverService is an implementation of ConfigResolver for JSON configuration.
type JsonConfigResolverService struct {
	configToResolve string
}

func NewJsonConfigResolverService() *JsonConfigResolverService {
	return &JsonConfigResolverService{}
}

func (s *JsonConfigResolverService) SetConfigToResolve(config string) {
	s.configToResolve = config
}

func (s *JsonConfigResolverService) ResolveConfig(userGroups []string) (string, error) {
	if s.configToResolve == "" {
		return "", ErrConfigNotSet
	}
	return s.ResolveConfigFrom(s.configToResolve, userGroups)
}

func (s *JsonConfigResolverService) ResolveConfigAs(userGroups []string, result interface{}) error {
	if s.configToResolve == "" {
		return ErrConfigNotSet
	}
	return s.ResolveConfigFromAs(s.configToResolve, userGroups, result)
}

func (s *JsonConfigResolverService) ResolveConfigFrom(config string, userGroups []string) (string, error) {
	var out map[string]interface{}
	if err := s.resolve(config, userGroups, &out); err != nil {
		return "", err
	}
	data, err := json.Marshal(out)
	return string(data), err
}

func (s *JsonConfigResolverService) ResolveConfigFromAs(config string, userGroups []string, result interface{}) error {
	var out map[string]interface{}
	if err := s.resolve(config, userGroups, &out); err != nil {
		return err
	}
	data, err := json.Marshal(out)
	if err != nil {
		return err
	}
	return json.Unmarshal(data, result)
}

func (s *JsonConfigResolverService) resolve(config string, userGroups []string, out *map[string]interface{}) error {
	var cfg dto.Config
	if err := json.Unmarshal([]byte(config), &cfg); err != nil {
		return err
	}
	groups := make(map[string]bool)
	if cfg.DefaultProperties == nil {
		return fmt.Errorf("invalid config: missing default-properties")
	}
	for _, g := range userGroups {
		groups[g] = true
	}
	for _, rule := range cfg.OverrideRules {
		applies, err := ruleApplies(groups, rule)
		if err != nil {
			return err
		}
		if applies {
			applyOverride(cfg.DefaultProperties, rule.Override)
		}
	}
	*out = cfg.DefaultProperties
	return nil
}

func ruleApplies(groups map[string]bool, rule dto.OverrideRule) (bool, error) {
	if len(rule.UserIsInAllGroups) > 0 {
		match := true
		for _, g := range rule.UserIsInAllGroups {
			if !groups[g] {
				match = false
				break
			}
		}
		if match {
			return true, nil
		}
	}
	if len(rule.UserIsInAnyGroup) > 0 {
		for _, g := range rule.UserIsInAnyGroup {
			if groups[g] {
				return true, nil
			}
		}
	}
	if len(rule.UserIsInNoneOfTheGroups) > 0 {
		none := true
		for _, g := range rule.UserIsInNoneOfTheGroups {
			if groups[g] {
				none = false
				break
			}
		}
		if none {
			return true, nil
		}
	}
	if rule.CustomExpression != "" {
		val, err := evaluateExpression(rule.CustomExpression, groups)
		if err != nil {
			return false, err
		}
		if val {
			return true, nil
		}
	}
	return false, nil
}

func applyOverride(base map[string]interface{}, override map[string]interface{}) {
	for k, v := range override {
		overrideProperty(base, k, v)
	}
}

func overrideProperty(node map[string]interface{}, path string, value interface{}) {
	parts := strings.Split(path, ".")
	for i, p := range parts {
		if i == len(parts)-1 {
			node[p] = value
			return
		}
		next, ok := node[p]
		if !ok {
			newMap := make(map[string]interface{})
			node[p] = newMap
			node = newMap
			continue
		}
		m, ok := next.(map[string]interface{})
		if !ok {
			m = make(map[string]interface{})
			node[p] = m
		}
		node = m
	}
}

var containsRe = regexp.MustCompile(`#user\.contains\('([^']+)'\)`) // pattern for user.contains('group')

func evaluateExpression(expr string, groups map[string]bool) (bool, error) {
	replaced := containsRe.ReplaceAllStringFunc(expr, func(s string) string {
		m := containsRe.FindStringSubmatch(s)
		if len(m) == 2 && groups[m[1]] {
			return "true"
		}
		return "false"
	})
	replaced = strings.ReplaceAll(replaced, " and ", " && ")
	replaced = strings.ReplaceAll(replaced, " or ", " || ")
	replaced = strings.ReplaceAll(replaced, " not ", " ! ")

	parsed, err := parser.ParseExpr(replaced)
	if err != nil {
		return false, err
	}
	return evalBoolAST(parsed)
}

func evalBoolAST(e ast.Expr) (bool, error) {
	switch v := e.(type) {
	case *ast.ParenExpr:
		return evalBoolAST(v.X)
	case *ast.UnaryExpr:
		if v.Op != token.NOT {
			return false, fmt.Errorf("unsupported unary op")
		}
		val, err := evalBoolAST(v.X)
		if err != nil {
			return false, err
		}
		return !val, nil
	case *ast.BinaryExpr:
		left, err := evalBoolAST(v.X)
		if err != nil {
			return false, err
		}
		right, err := evalBoolAST(v.Y)
		if err != nil {
			return false, err
		}
		switch v.Op {
		case token.LAND:
			return left && right, nil
		case token.LOR:
			return left || right, nil
		default:
			return false, fmt.Errorf("unsupported binary op")
		}
	case *ast.Ident:
		if v.Name == "true" {
			return true, nil
		}
		if v.Name == "false" {
			return false, nil
		}
		return false, fmt.Errorf("unknown identifier %s", v.Name)
	default:
		return false, fmt.Errorf("unsupported expr")
	}
}
