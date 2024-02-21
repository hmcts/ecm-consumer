module "postgres" {
  source = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=dtspo-16806-schema-owner"
  env    = var.env
  providers = {
    azurerm.postgres_network = azurerm.private_endpoint
  }
  name          = "ecm-consumer-postgres-v15"
  product       = var.product
  component     = var.component
  business_area = var.businessArea
  common_tags   = local.common_tags
  subnet_suffix = "expanded"
  pgsql_databases = [
    {
      name : "ecmconsumer"
    }
  ]
  pgsql_version        = "15"
  admin_user_object_id = var.jenkins_AAD_objectId

  # force_schema_ownership_trigger = "1"
  kv_subscription = var.subscription
  kv_name = data.azurerm_key_vault.ethos_key_vault.name
  user_secret_name = data.azurerm_key_vault_secret.ecm_postgres_user.name
  pass_secret_name = data.azurerm_key_vault_secret.ecm_postgres_password.name
}

resource "azurerm_key_vault_secret" "ecm_consumer_postgres_user_v15" {
  name         = "ecm-consumer-postgres-user-v15"
  value        = module.postgres.username
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}

resource "azurerm_key_vault_secret" "ecm_consumer_postgres_password_v15" {
  name         = "ecm-consumer-postgres-password-v15"
  value        = module.postgres.password
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}

resource "azurerm_key_vault_secret" "ecm_consumer_postgres_host_v15" {
  name         = "ecm-consumer-postgres-host-v15"
  value        = module.postgres.fqdn
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}

resource "azurerm_key_vault_secret" "ecm_consumer_postgres_port_v15" {
  name         = "ecm-consumer-postgres-port-v15"
  value        = "5432"
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}
