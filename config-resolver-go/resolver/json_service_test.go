package resolver

import (
	"path/filepath"
	"strings"
	"testing"
)

type TestDTOProperty3dash1 struct {
	Property3Dash1Dash1 bool `json:"property3-1-1"`
}

type TestDTOProperty3 struct {
	Property3Dash1 TestDTOProperty3dash1 `json:"property3-1"`
}

type TestDTOProperty2 struct {
	Property2Dash1 bool `json:"property2-1"`
}

type TestDTO struct {
	Property1 int              `json:"property1"`
	Property2 TestDTOProperty2 `json:"property2"`
	Property3 TestDTOProperty3 `json:"property3"`
}

func buildPath(parts ...string) string {
	return filepath.Join(append([]string{"testdata"}, parts...)...)
}

func prepareService() *JsonConfigResolverService {
	return NewJsonConfigResolverService()
}

func TestResolveConfigScenarios(t *testing.T) {
	tests := []struct {
		groups  []string
		inFile  string
		outFile string
	}{
		{[]string{"group-a", "group-b"}, buildPath("user-in-all-groups", "input.json"), buildPath("user-in-all-groups", "output.json")},
		{[]string{"group-d"}, buildPath("user-in-any-groups", "input.json"), buildPath("user-in-any-groups", "output.json")},
		{[]string{"group-c"}, buildPath("user-in-no-groups", "input.json"), buildPath("user-in-no-groups", "output.json")},
		{[]string{"group-a", "group-b", "group-c"}, buildPath("user-in-different-groups", "input.json"), buildPath("user-in-different-groups", "output.json")},
		{[]string{"group-a", "group-b", "group-c"}, buildPath("custom-user-groups", "input.json"), buildPath("custom-user-groups", "output.json")},
	}

	svc := prepareService()

	for _, tt := range tests {
		inputCfg, err := readFile(tt.inFile)
		if err != nil {
			t.Fatal(err)
		}
		var expected TestDTO
		if err := readFileIntoObject(tt.outFile, &expected); err != nil {
			t.Fatal(err)
		}

		var actual TestDTO
		if err := svc.ResolveConfigFromAs(inputCfg, tt.groups, &actual); err != nil {
			t.Fatal(err)
		}
		if expected != actual {
			t.Errorf("unexpected result for %v", tt)
		}
		outStr, err := svc.ResolveConfigFrom(inputCfg, tt.groups)
		if err != nil {
			t.Fatal(err)
		}
		expectedStr, err := readFile(tt.outFile)
		if err != nil {
			t.Fatal(err)
		}
		if normalize(outStr) != normalize(expectedStr) {
			t.Errorf("string result mismatch for %v", tt)
		}
	}
}

func normalize(s string) string {
	s = strings.ReplaceAll(s, "\n", "")
	s = strings.ReplaceAll(s, " ", "")
	return s
}

func TestInvalidConfig(t *testing.T) {
	svc := prepareService()
	groups := []string{"group-a", "group-b"}
	input, err := readFile(buildPath("invalid-config", "input.json"))
	if err != nil {
		t.Fatal(err)
	}
	if _, err := svc.ResolveConfigFrom(input, groups); err == nil {
		t.Errorf("expected error")
	}
	if err := svc.ResolveConfigFromAs(input, groups, &TestDTO{}); err == nil {
		t.Errorf("expected error")
	}
	svc.SetConfigToResolve(input)
	if _, err := svc.ResolveConfig(groups); err == nil {
		t.Errorf("expected error")
	}
	if err := svc.ResolveConfigAs(groups, &TestDTO{}); err == nil {
		t.Errorf("expected error")
	}
}
