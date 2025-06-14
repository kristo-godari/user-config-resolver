package resolver

import "errors"

// ConfigResolver defines methods for resolving configuration for user groups.
type ConfigResolver interface {
	SetConfigToResolve(config string)
	ResolveConfig(userGroups []string) (string, error)
	ResolveConfigAs(userGroups []string, result interface{}) error
	ResolveConfigFrom(config string, userGroups []string) (string, error)
	ResolveConfigFromAs(config string, userGroups []string, result interface{}) error
}

var ErrConfigNotSet = errors.New("config to resolve is null. Use SetConfigToResolve() method to set the config")
