provider "azurerm" {
  version = "1.23.0"
}

locals {
  app = "ecm-consumer"

  previewVaultNameAndGroup = "${var.product}-shared-aat"
  nonPreviewVaultNameAndGroup = "${var.product}-shared-${var.env}"
  vaultNameAndGroup = "${var.env == "preview" ? local.previewVaultNameAndGroup : local.nonPreviewVaultNameAndGroup}"
  vaultUri = "${data.azurerm_key_vault.ethos_key_vault.vault_uri}"

}

module "ecm-consumer" {
  source                          = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product                         = "${var.product}-${local.app}"
  location                        = "${var.location}"
  env                             = "${var.env}"
  ilbIp                           = "${var.ilbIp}"
  subscription                    = "${var.subscription}"
  capacity                        = "${var.capacity}"
  common_tags                     = "${var.common_tags}"
  is_frontend                     = false
  enable_ase                      = "${var.enable_ase}"

  app_settings                         = {
    WEBSITE_PROACTIVE_AUTOHEAL_ENABLED = "${var.autoheal}"
    ECM_S2S_SECRET_KEY               = "${data.azurerm_key_vault_secret.ecm-consumer-s2s-secret.value}"
    IDAM_API_URL                       = "${var.idam_api_url}"
    IDAM_API_JWK_URL                   = "${var.idam_api_url}/jwks"
    SERVICE_AUTH_PROVIDER_URL          = "${var.s2s_url}"
    MICRO_SERVICE                      = "${var.micro_service}"
  }
}

resource "azurerm_key_vault_secret" "AZURE_APPINSGHTS_KEY" {
  name         = "AppInsightsInstrumentationKey"
  value        = "${azurerm_application_insights.appinsights.instrumentation_key}"
  key_vault_id = "${data.azurerm_key_vault.ethos_key_vault.id}"
}

resource "azurerm_application_insights" "appinsights" {
  name                = "${var.product}-${var.component}-appinsights-${var.env}"
  location            = "${var.appinsights_location}"
  resource_group_name = "${local.vaultNameAndGroup}"
  application_type    = "Web"

  tags = "${var.common_tags}"
}

data "azurerm_key_vault" "ethos_key_vault" {
  name                = "${local.vaultNameAndGroup}"
  resource_group_name = "${local.vaultNameAndGroup}"
}

data "azurerm_key_vault_secret" "ecm-consumer-s2s-secret" {
  name = "ecm-consumer-s2s-secret"
  key_vault_id = "${data.azurerm_key_vault.ethos_key_vault.id}"
}
