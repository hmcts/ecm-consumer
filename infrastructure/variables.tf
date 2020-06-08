variable "product" {}

variable "component" {
  type = string
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
  type        = string
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
  type                        = string
  description                 = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "caseworker_user_name" {
  type        = string
  default     = "eric.ccdcooper@gmail.com"
  description = "User name of the worker to send event updates and emails"
}

variable "caseworker_password" {
  type        = string
  default     = "Nagoya0102"
  description = "Password of the worker to send event updates and emails"
}

variable "ilbIp" {}

variable "team_name" {
  type        = string
  description = "Team name"
  default     = "ECM Project"
}

variable "team_contact" {
  type        = string
  description = "Team contact"
  default     = "#ethos-repl-service"
}

variable "destroy_me" {
  type        = string
  description = "Here be dragons! In the future if this is set to Yes then automation will delete this resource on a schedule. Please set to No unless you know what you are doing"
  default     = "No"
}
