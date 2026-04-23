# Feat-001 - Agent Child: build modelfile

## Status

state: proposed
user_history: 
depends_on:
owned_by: agents
test: Agent child model should be able on Ollama service and return a basic response.

## Description

The goal of this feature is to create a model file that describes an agent. Agents communicate through events with predefined output and input schemes. The model must be able to handle incoming events and return responses in the specified format. 

Within the same feature, the agent's input and output formats must be documented so that it is clear how to interact with it. 

The necessary scripts and files will also be created for the agent to load into the Ollama service and start receiving requests.

### Risks
- Hallusion risk: The agent might generate incorrect or misleading information. This could happen if the agent does not have enough context or if it is not properly
- PII Exposed
- Out of scope for  the agent to handle.
- Prompt injection 


### Input format

proposed
'''json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "education-framework-agent-child-v1",
  "type": "object",
  "required": ["version", "response_type", "content_text", "content_type"],
  "properties": {
    "version": { "type": "string", "pattern": "^v\\d+" },
    "response_type": { "type": "string", "enum": ["narration","prompt","action","tool_call","refusal"] },
    "content_text": { "type": "string", "maxLength": 300 },
    "content_type": { "type": "string", "enum": ["plain_text","tts_snippet","structured_activity"] },
    "suggested_actions": { "type": "array", "items": { "type": "string" }, "maxItems": 5 },
    "safety_flags": { "type": "array", "items": { "type": "string", "enum": ["age_inappropriate","pii_detected","out_of_scope","needs_parent_attention"] } },
    "tool_calls": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["tool_name"],
        "properties": {
          "tool_name": { "type": "string" },
          "inputs": { "type": "object" },
          "note": { "type": "string" }
        },
        "additionalProperties": false
      },
      "maxItems": 3
    },
    "confidence_score": { "type": "number", "minimum": 0, "maximum": 1 }
  },
  "additionalProperties": false
}
'''

### Output Format

Valid response format:

'''json
{
  "version": "v1",
  "response_type": "narration",
  "content_text": "¡Genial trabajo terminando la actividad! ¿Quieres jugar otra vez o probar un reto nuevo?",
  "content_type": "tts_snippet",
  "suggested_actions": ["play_again","try_new_challenge"],
  "safety_flags": [],
  "tool_calls": []
}
'''
Invalid response format:
'''
{
  "version": "v1",
  "response_type": "refusal",
  "content_text": "Esa pregunta la debe responder un adulto. He avisado a los padres para que lo revisen.",
  "content_type": "plain_text",
  "suggested_actions": ["notify_parent"],
  "safety_flags": ["needs_parent_attention"],
  "tool_calls": [
    { "tool_name": "send_parent_notification", "inputs": { "reason": "out_of_scope_question" }, "note": "backend must gate/send" }
  ]
}
'''


