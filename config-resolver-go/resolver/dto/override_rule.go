package dto

// OverrideRule defines the conditions and properties for override

type OverrideRule struct {
	UserIsInAllGroups       []string               `json:"user-is-in-all-groups"`
	UserIsInAnyGroup        []string               `json:"user-is-in-any-group"`
	UserIsInNoneOfTheGroups []string               `json:"user-is-none-of-the-groups"`
	CustomExpression        string                 `json:"custom-expression"`
	Override                map[string]interface{} `json:"override"`
}
