# Kubernetes Deployment for Librarie with Alloy Logging

This directory contains the Kubernetes manifests for deploying the Librarie application with Grafana Alloy for log collection.

## Overview

The deployment includes:

- **Alloy**: Grafana's observability agent for collecting logs from Librarie application pods
- **Namespace**: `librarie` namespace for all resources
- **RBAC**: Service account and permissions for Alloy to discover pods
- **Security**: Alloy runs with `fsGroup: 190` as required

## Key Security Configuration

The Alloy deployment includes the following security context configuration:

```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 10001
  runAsGroup: 10001
  fsGroup: 190  # Required filesystem group
  seccompProfile:
    type: RuntimeDefault
```

This ensures that:
- Alloy runs as a non-root user
- Files created by Alloy have the correct group ownership (GID 190)
- The container runs with minimal privileges

## Files

- `namespace.yaml` - Creates the librarie namespace
- `alloy/alloy-rbac.yaml` - ServiceAccount and RBAC permissions for Alloy
- `alloy/alloy-config.yaml` - ConfigMap with Alloy configuration for log collection
- `alloy/alloy-deployment.yaml` - Alloy deployment with fsGroup: 190
- `alloy/alloy-service.yaml` - Service for Alloy metrics endpoint
- `app/librarie-deployment.yaml` - Sample application deployment
- `kustomization.yaml` - Kustomize configuration for easy deployment

## Deployment

### Prerequisites

1. Kubernetes cluster with RBAC enabled
2. Loki instance for log storage (configure LOKI_URL in the deployment)
3. Optional: Loki credentials secret

### Deploy using kubectl

```bash
# Deploy all resources
kubectl apply -k .

# Verify deployment
kubectl get pods -n librarie
kubectl get services -n librarie
```

### Deploy using kustomize

```bash
# Build and apply
kustomize build . | kubectl apply -f -

# Verify fsGroup is set correctly
kubectl get deployment alloy -n librarie -o yaml | grep fsGroup
```

## Configuration

### Alloy Configuration

The Alloy configuration (`alloy-config.yaml`) is designed to:

1. Discover pods in the `librarie` namespace
2. Collect logs from pods labeled with `app: librarie`
3. Parse JSON logs from Quarkus applications
4. Add metadata (pod name, namespace, container)
5. Forward logs to Loki

### Environment Variables

Configure these environment variables in the Alloy deployment:

- `LOKI_URL`: URL of the Loki instance (e.g., `http://loki:3100/loki/api/v1/push`)
- `LOKI_USERNAME`: Optional username for Loki authentication
- `LOKI_PASSWORD`: Optional password for Loki authentication

### Log Format

The configuration expects Quarkus applications to output JSON structured logs. The application should be configured with:

```properties
quarkus.log.console.format=%d{HH:mm:ss} %-5p traceId=%X{traceId}, parentId=%X{parentId}, spanId=%X{spanId}, sampled=%X{sampled} [%c{2.}] (%t) %s%e%n
quarkus.otel.logs.enabled=true
```

## Monitoring

Alloy exposes metrics on port 12345:

- Health check: `http://alloy:12345/-/healthy`
- Ready check: `http://alloy:12345/-/ready`
- Metrics: `http://alloy:12345/metrics`

## Security Considerations

1. **fsGroup: 190**: Ensures proper file permissions for log files
2. **Non-root user**: Alloy runs as user 10001
3. **Read-only root filesystem**: Container filesystem is read-only except for specific mount points
4. **Dropped capabilities**: All Linux capabilities are dropped
5. **seccomp profile**: Uses RuntimeDefault seccomp profile

## Troubleshooting

### Check Alloy logs
```bash
kubectl logs -n librarie deployment/alloy
```

### Check Alloy configuration
```bash
kubectl get configmap alloy-config -n librarie -o yaml
```

### Verify fsGroup setting
```bash
kubectl describe pod -n librarie -l app=alloy | grep -A 10 "Security Context"
```

### Test log collection
```bash
# Generate test logs from the application
kubectl exec -n librarie deployment/librarie-backend -- logger "Test log message"

# Check if logs appear in Loki (if accessible)
curl -G -s "http://loki:3100/loki/api/v1/query" --data-urlencode 'query={job="librarie"}'
```