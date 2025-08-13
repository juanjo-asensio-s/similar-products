Feature: Similar products errors

  Background:
    * def appBaseUrl = karate.properties['app.baseUrl']
    * print 'APP BASE URL =>', appBaseUrl
    * url appBaseUrl

  Scenario: 404 (Product not found)
    Given path 'product', '50', 'similar'
    When method get
    Then status 404
    And match response contains { code: '#string', detail: '#string' }

  Scenario: 502 (Upstream failure)
    Given path 'product', '1000', 'similar'
    When method get
    Then status 502
    And match response contains { code: '#string', detail: '#string' }
