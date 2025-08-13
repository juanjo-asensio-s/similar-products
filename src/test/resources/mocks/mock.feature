Feature: Mock externo Product API (happy path + errores)

  Background:
    * configure responseHeaders = { 'Content-Type': 'application/json' }

# -------- HAPPY PATH (product 1) --------
  Scenario: pathMatches('/product/1/similarids') && methodIs('get')
    * def response = [2,3,4]

  Scenario: pathMatches('/product/2') && methodIs('get')
    * def response = { id: '2', name: 'Dress',  price: 19.99, availability: true }

  Scenario: pathMatches('/product/3') && methodIs('get')
    * def response = { id: '3', name: 'Blazer', price: 29.99, availability: false }

  Scenario: pathMatches('/product/4') && methodIs('get')
    * def response = { id: '4', name: 'Boots',  price: 39.99, availability: true }

# -------- ERRORS FEATURE: "partial failure" (product 5) --------
# similarids -> [1,2,6] y /product/6 da 500 (se ignora)
  Scenario: pathMatches('/product/5/similarids') && methodIs('get')
    * def response = [1,2,6]

  Scenario: pathMatches('/product/1') && methodIs('get')
    * def response = { id: '1', name: 'Shirt', price: 9.99, availability: true }

# /product/2 ya está definido arriba

  Scenario: pathMatches('/product/6') && methodIs('get')
    * def responseStatus = 500
    * def response = { message: 'Upstream error for product 6' }

# -------- ERRORS FEATURE: "timeouts_and_slow_deps" (product 3) --------
  Scenario: pathMatches('/product/3/similarids') && methodIs('get')
    * def response = [100,1000,10000]

  Scenario: pathMatches('/product/100') && methodIs('get')
    * def response = { id: '100', name: 'Trousers', price: 49.99, availability: false }

  Scenario: pathMatches('/product/1000') && methodIs('get')
    * configure responseDelay = 2500   # ajusta > read-timeout del cliente
    * def response = { id: '1000', name: 'Slow Jacket', price: 89.00, availability: true }

  Scenario: pathMatches('/product/10000') && methodIs('get')
    * configure responseDelay = 2500
    * def response = { id: '10000', name: 'Slow Boots', price: 129.00, availability: true }

# -------- NOT FOUND específico para el errors feature --------
  Scenario: pathMatches('/product/50/similarids') && methodIs('get')
    * def responseStatus = 404
    * def response = { message: 'Similar IDs not found for 50' }

# -------- fallback: cualquier otra ruta no mockeada → 404 --------
  Scenario: methodIs('get')
    * def msg = 'No mock defined for ' + request.path
    * def responseStatus = 404
    * def response = { message: '#(msg)' }
