variable "product" {}

variable "component" {
}

variable "location" {
  default = "UK South"
}

variable "env" {}

variable "subscription" {}

variable "deployment_namespace" {}

variable "common_tags" {
  type = map(string)
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

variable "ccd_data_store_api_url" {
  default = "http://ccd-data-store-api:4452"
}

variable "jenkins_AAD_objectId" {
  description  = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "ilbIp" {}

variable "team_name" {
  description = "Team name"
  default     = "ECM Project"
}

variable "team_contact" {
  description = "Team contact"
  default     = "#ethos-repl-service"
}

variable "destroy_me" {
  description = "Here be dragons! In the future if this is set to Yes then automation will delete this resource on a schedule. Please set to No unless you know what you are doing"
  default     = "No"
}
