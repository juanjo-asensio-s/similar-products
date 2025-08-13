Feature: E2E - /product/{id}/similar

  Background:
    * def appBaseUrl = karate.properties['app.baseUrl']
    * print 'APP BASE URL =>', appBaseUrl
    * url appBaseUrl


    Given path 'product', '1', 'similar'
    When method get
    Then status 200
    And match response contains only
"""
[
  { id: '2', name: 'Dress',  price: 19.99, availability: true  },
  { id: '3', name: 'Blazer', price: 29.99, availability: false },
  { id: '4', name: 'Boots',  price: 39.99, availability: true  }
]
"""


  Scenario: Product ID 5, one similar product is ignored
    Given path 'product', '5', 'similar'
    When method get
    Then status 200
    And match each response == { id: '#string', name: '#string', price: '#number', availability: '#boolean' }
    And match response contains deep { id: '1', name: 'Shirt',  price: 9.99, availability: true }
    And match response contains deep { id: '2', name: 'Dress',  price: 19.99, availability: true }


  Scenario: High timeout
    Given path 'product', '3', 'similar'
    When method get
    Then status 200
    And match response contains any { id: '100', name: 'Trousers', price: 49.99, availability: false }
