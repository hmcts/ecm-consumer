
output "app_namespace" {
  value = var.deployment_namespace
}

output "vaultName" {
  value = local.vaultName
}

output "vaultUri" {
  value = local.vaultUri
}

output "idam_api_url" {
  value = var.idam_api_url
}

output "ccd_data_store_api_url" {
  value = var.ccd_data_store_api_url
}
