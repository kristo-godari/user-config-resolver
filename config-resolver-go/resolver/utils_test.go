package resolver

import (
	"encoding/json"
	"io/ioutil"
)

func readFile(p string) (string, error) {
	b, err := ioutil.ReadFile(p)
	if err != nil {
		return "", err
	}
	return string(b), nil
}

func readFileIntoObject(p string, out interface{}) error {
	data, err := ioutil.ReadFile(p)
	if err != nil {
		return err
	}
	return json.Unmarshal(data, out)
}
