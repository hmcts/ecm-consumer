provider "azurerm" {
  features {}
}

locals {
  previewVaultName    = "${var.product}-shared-aat"
  nonPreviewVaultName = "${var.product}-shared-${var.env}"
  vaultName           = var.env == "preview" ? local.previewVaultName : local.nonPreviewVaultName
  vaultUri            = data.azurerm_key_vault.ethos_key_vault.vault_uri
  previewRG           = "${var.product}-aat"
  nonPreviewRG        = "${var.product}-${var.env}"
  resourceGroup       = var.env == "preview" ? local.previewRG : local.nonPreviewRG
  localEnv            = var.env == "preview" ? "aat" : var.env
  s2sRG               = "rpe-service-auth-provider-${local.localEnv}"
  tagEnv              = var.env == "aat" ? "staging" : var.env == "perftest" ? "testing" : var.env
  common_tags = {
    "environment"  = local.tagEnv
    "Team Name"    = var.team_name
    "Team Contact" = var.team_contact
    "Destroy Me"   = var.destroy_me
    "application"  = var.product
    "builtFrom"    = "https://github.com/HMCTS/ecm-consumer.git"
  }
  tags = merge(local.common_tags, tomap({ "lastUpdated" = timestamp() }))
}

data "azurerm_key_vault" "ethos_key_vault" {
  name                = local.vaultName
  resource_group_name = local.resourceGroup
}

data "azurerm_key_vault" "s2s_key_vault" {
  name                = "s2s-${local.localEnv}"
  resource_group_name = local.s2sRG
}

resource "azurerm_key_vault_secret" "AZURE_APPINSGHTS_KEY" {
  name         = "AppInsightsInstrumentationKey"
  value        = azurerm_application_insights.appinsights.instrumentation_key
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}

resource "azurerm_application_insights" "appinsights" {
  name                = "${var.product}-${var.component}-appinsights-${var.env}"
  location            = var.appinsights_location
  resource_group_name = local.resourceGroup
  application_type    = "web"

  tags = var.common_tags

  lifecycle {
    ignore_changes = [
      # Ignore changes to appinsights as otherwise upgrading to the Azure provider 2.x
      # destroys and re-creates this appinsights instance
      application_type,
    ]
  }
}

data "azurerm_key_vault_secret" "microservicekey_ecm_consumer" {
  name         = "microservicekey-ecm-consumer"
  key_vault_id = data.azurerm_key_vault.s2s_key_vault.id
}

resource "azurerm_key_vault_secret" "ecm_consumer_s2s_key" {
  name         = "ecm-consumer-s2s-key"
  value        = data.azurerm_key_vault_secret.microservicekey_ecm_consumer.value
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}

# SERVICE BUS
data "azurerm_key_vault_secret" "create_updates_queue_send_conn_str" {
  name         = "create-updates-queue-send-connection-string"
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}

data "azurerm_key_vault_secret" "create_updates_queue_listen_conn_str" {
  name         = "create-updates-queue-listen-connection-string"
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}

data "azurerm_key_vault_secret" "update_case_queue_send_conn_str" {
  name         = "update-case-queue-send-connection-string"
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}

data "azurerm_key_vault_secret" "update_case_queue_listen_conn_str" {
  name         = "update-case-queue-listen-connection-string"
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}

# DB
data "azurerm_key_vault_secret" "ecm_postgres_user" {
  name         = "ecm-consumer-postgres-user"
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}

data "azurerm_key_vault_secret" "ecm_postgres_password" {
  name         = "ecm-consumer-postgres-password"
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}

data "azurerm_key_vault_secret" "ecm_postgres_host" {
  name         = "ecm-consumer-postgres-host"
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}

data "azurerm_key_vault_secret" "ecm_postgres_port" {
  name         = "ecm-consumer-postgres-port"
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}

data "azurerm_key_vault_secret" "ecm_postgres_database" {
  name         = "ecm-consumer-postgres-database"
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}
