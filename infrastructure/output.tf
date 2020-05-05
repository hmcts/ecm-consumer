
output "app_namespace" {
  value = "${var.deployment_namespace}"
}

output "vaultName" {
  value = "${local.vaultNameAndGroup}"
}

output "vaultUri" {
  value = "${local.vaultUri}"
}

output "idam_api_url" {
  value = "${var.idam_api_url}"
}
