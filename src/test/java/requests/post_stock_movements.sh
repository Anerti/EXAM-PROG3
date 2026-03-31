#!/bin/bash

echo "Testing POST /ingredients/{id}/stockMovements"

echo "--- Test 1: Create stock movement IN for ingredient 1 ---"
curl -s -X POST -i \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 5.0,
    "unit": "KG",
    "type": "IN",
    "creationDatetime": "2024-01-10T10:00:00Z"
  }' \
  http://localhost:8080/ingredients/1/stockMovements

echo ""
echo "--- Test 2: Create stock movement OUT for ingredient 3 ---"
curl -s -X POST -i \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 2.0,
    "unit": "KG",
    "type": "OUT",
    "creationDatetime": "2024-01-11T14:00:00Z"
  }' \
  http://localhost:8080/ingredients/3/stockMovements

echo ""
echo "--- Test 3: Missing request body (should fail) ---"
curl -s -X POST -i \
  -H "Content-Type: application/json" \
  -d '{}' \
  http://localhost:8080/ingredients/1/stockMovements

echo ""
echo "--- Test 4: Missing quantity (should fail) ---"
curl -s -X POST -i \
  -H "Content-Type: application/json" \
  -d '{
    "unit": "KG",
    "type": "IN"
  }' \
  http://localhost:8080/ingredients/1/stockMovements

echo ""
echo "--- Test 5: Invalid ingredient id (should fail) ---"
curl -s -X POST -i \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 5.0,
    "unit": "KG",
    "type": "IN"
  }' \
  http://localhost:8080/ingredients/999/stockMovements
