#!/bin/bash

echo "Testing GET /ingredients/{id}/stockMovements?from={from}&to={to}"

FROM="2024-01-01T00:00:00Z"
TO="2024-12-31T23:59:59Z"

echo "--- Test 1: Get stock movements for ingredient 1 ---"
curl -s -w "\nHTTP Status: %{http_code}\n" "http://localhost:8080/ingredients/1/stockMovements?from=${FROM}&to=${TO}"

echo ""
echo "--- Test 2: Get stock movements for ingredient 2 ---"
curl -s -w "\nHTTP Status: %{http_code}\n" "http://localhost:8080/ingredients/2/stockMovements?from=${FROM}&to=${TO}"

echo ""
echo "--- Test 3: Missing 'from' parameter (should fail) ---"
curl -s -w "\nHTTP Status: %{http_code}\n" "http://localhost:8080/ingredients/1/stockMovements?to=${TO}"

echo ""
echo "--- Test 4: Missing 'to' parameter (should fail) ---"
curl -s -w "\nHTTP Status: %{http_code}\n" "http://localhost:8080/ingredients/1/stockMovements?from=${FROM}"

echo ""
echo "--- Test 5: Non-existent ingredient (should fail) ---"
curl -s -w "\nHTTP Status: %{http_code}\n" "http://localhost:8080/ingredients/999/stockMovements?from=${FROM}&to=${TO}"
