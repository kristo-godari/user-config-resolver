package dto

// Config describes the configuration input for Json resolver

type Config struct {
	OverrideRules     []OverrideRule         `json:"override-rules"`
	DefaultProperties map[string]interface{} `json:"default-properties"`
}
