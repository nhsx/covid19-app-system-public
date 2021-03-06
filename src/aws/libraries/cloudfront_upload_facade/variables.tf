variable "name" {
  description = "Name of the CloudFront distribution"
}

variable "risky-post-districts-upload-endpoint" {
  description = "The endpoint for risky post districts upload"
}

variable "risky-post-districts-upload-path" {
  description = "The route to the risky post districts upload"
}

variable "risky-venues-upload-endpoint" {
  description = "The endpoint for risky venues upload"
}

variable "risky-venues-upload-path" {
  description = "The route to the risky post districts upload"
}

variable "test-results-upload-endpoint" {
  description = "The endpoint for test results upload"
}

variable "test-results-upload-path" {
  description = "The route to the test results upload"
}
variable "isolation_payment_endpoint" {
  description = "The endpoint for the isolation payment submission API"
}
variable "isolation_payment_path" {
  description = "The route for the isolation payment submission"
}
variable "isolation_payment_health_path" {
  description = "The route for the isolation payment health endpoint"
}

variable "domain" {
  description = "The domain the CloudFront distribution needs to be deployed into"
}

variable "web_acl_arn" {
  description = "The ARN of the WAFv2 web acl to filter CloudFront requests"
}

variable "custom_oai" {
  description = "Secret shared between CloudFront Distribution and Lambda"
}

variable "enable_shield_protection" {
  description = "Flag to enable/disable shield protection"
  type        = bool
}

variable "risky-post-districts-upload-health-path" {
  description = "The route to the risky post districts health endpoint"
}

variable "risky-venues-upload-health-path" {
  description = "The route to the risky venues health endpoint"
}

variable "tags" {
  description = "A map of key-value labels used to tag AWS resources"
  type        = map(string)
}
