variable "product" {}

variable "component" {
  type = "string"
}

variable "location" {
  default = "UK South"
}

variable "env" {}

variable "subscription" {}

variable "deployment_namespace" {}

variable "common_tags" {
  type = "map"
}

variable "micro_service" {
  default = "ecm_consumer"
}

variable "managed_identity_object_id" {
  default = ""
}

variable "idam_api_url" {
  default = "http://sidam-api:5000"
}

variable "appinsights_location" {
  type        = "string"
  default     = "West Europe"
  description = "Location for Application Insights"
}

variable "enable_ase" {
  default = false
}

variable "tenant_id" {}

variable "capacity" {
  default = "1"
}

variable "s2s_url" {
  default = "http://service-auth-provider-api:8080"
}

variable "jenkins_AAD_objectId" {
  type                        = "string"
  description                 = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}
