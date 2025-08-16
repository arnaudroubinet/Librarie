#!/bin/bash
# Test script to validate Alloy configuration with fsGroup: 190

set -e

echo "=== Validating Alloy Kubernetes Configuration ==="
echo

# Check if kustomize is available
if ! command -v kustomize &> /dev/null; then
    echo "ERROR: kustomize is required but not installed"
    exit 1
fi

cd "$(dirname "$0")"

echo "1. Building Kubernetes manifests with kustomize..."
kustomize build . > /tmp/k8s-manifests.yaml
echo "✓ Manifests built successfully"

echo
echo "2. Checking for required fsGroup: 190..."
if grep -q "fsGroup: 190" /tmp/k8s-manifests.yaml; then
    echo "✓ fsGroup: 190 found in manifests"
else
    echo "✗ fsGroup: 190 NOT found in manifests"
    exit 1
fi

echo
echo "3. Validating Alloy deployment contains security context..."
if grep -A 10 -B 5 "fsGroup: 190" /tmp/k8s-manifests.yaml | grep -q "securityContext"; then
    echo "✓ Security context with fsGroup: 190 properly configured"
else
    echo "✗ Security context not properly configured"
    exit 1
fi

echo
echo "4. Checking for Alloy configuration components..."
components=(
    "kind: Namespace"
    "kind: ServiceAccount" 
    "kind: ClusterRole"
    "kind: ClusterRoleBinding"
    "kind: ConfigMap"
    "kind: Deployment"
    "kind: Service"
)

for component in "${components[@]}"; do
    if grep -q "$component" /tmp/k8s-manifests.yaml; then
        echo "✓ $component found"
    else
        echo "✗ $component missing"
        exit 1
    fi
done

echo
echo "5. Validating Alloy deployment has correct app label..."
if grep -A 20 "kind: Deployment" /tmp/k8s-manifests.yaml | grep -q "app: alloy"; then
    echo "✓ Alloy deployment has correct labels"
else
    echo "✗ Alloy deployment missing app label"
    exit 1
fi

echo
echo "6. Checking YAML syntax with basic validation..."
if python3 -c "import yaml; list(yaml.safe_load_all(open('/tmp/k8s-manifests.yaml')))" 2>/dev/null; then
    echo "✓ YAML syntax validation passed"
else
    echo "✗ YAML syntax validation failed"
    exit 1
fi

echo
echo "=== Validation Complete ==="
echo "✓ All checks passed! Alloy configuration is ready with fsGroup: 190"
echo
echo "To deploy:"
echo "  kubectl apply -k ."
echo
echo "To verify fsGroup after deployment:"
echo "  kubectl describe pod -n librarie -l app=alloy | grep -A 10 'Security Context'"