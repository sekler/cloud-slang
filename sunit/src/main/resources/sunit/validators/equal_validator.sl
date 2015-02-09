namespace: sunit.validators

operations:
  - equal_validator:
      inputs:
        - param
        - value
      action:
        python_script: |
          is_equal = (param == value)
          if not is_equal:
              print "Got: " + param + " expected: " + value
      results:
        - SUCCESS: is_equal == "True"
        - FAILURE

