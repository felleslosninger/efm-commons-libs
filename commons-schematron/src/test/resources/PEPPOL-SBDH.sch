<schema xmlns="http://purl.oclc.org/dsdl/schematron"
  schemaVersion="iso" queryBinding="xslt2">

  <title>Rules for PEPPOL SBDH</title>

  <ns uri="http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader" prefix="sbdh"/>

  <pattern>
    <rule context="sbdh:StandardBusinessDocument">
      <assert id="PEPPOL-SBDH-R001" test="sbdh:StandardBusinessDocumentHeader" flag="fatal">A header is required.</assert>
    </rule>
    <rule context="sbdh:StandardBusinessDocumentHeader">
      <assert id="PEPPOL-SBDH-R002" test="sbdh:HeaderVersion = '1.0'" flag="fatal">A header must be version '1.0'.</assert>
      <assert id="PEPPOL-SBDH-R003" test="sbdh:BusinessScope" flag="fatal">Business scope is required.</assert>
      <assert id="PEPPOL-SBDH-R004" test="not(sbdh:Manifest)" flag="fatal">Manifest is not allowed.</assert>
    </rule>
    <rule context="sbdh:Sender | sbdh:Receiver">
      <assert id="PEPPOL-SBDH-R012" test="not(sbdh:ContactInformation)" flag="fatal">Contact information element is not allowed.</assert>
    </rule>
    <rule context="sbdh:Sender/sbdh:Identifier | sbdh:Receiver/sbdh:Identifier">
      <assert id="PEPPOL-SBDH-R010" test="not(. = '')" flag="fatal">Identifier can not be empty.</assert>
      <assert id="PEPPOL-SBDH-R011" test="@Authority = 'iso6523-actorid-upis'" flag="fatal">Identifier must have authority set to 'iso6523-actorid-upis'.</assert>
    </rule>
    <rule context="sbdh:DocumentIdentification">
      <assert id="PEPPOL-SBDH-R020" test="not(sbdh:MultipleType)">Element 'MultipleType' not allowed.</assert>
    </rule>
    <rule context="sbdh:BusinessScope">
      <assert id="PEPPOL-SBDH-R030" test="sbdh:Scope[sbdh:Type = 'DOCUMENTID']" flag="fatal">Scope 'DOCUMENTID' is required.</assert>
      <assert id="PEPPOL-SBDH-R031" test="sbdh:Scope[sbdh:Type = 'PROCESSID']" flag="fatal">Scope 'PROCESSID' is required.</assert>
    </rule>
    <rule context="sbdh:BusinessScope/sbdh:Scope[sbdh:Type = 'DOCUMENTID']">
      <assert id="PEPPOL-SBDH-R040" test="not(sbdh:Identifier)" flag="fatal">Identifier is not allowed for DOCUMENTID.</assert>
      <assert id="PEPPOL-SBDH-R041" test="not(sbdh:CorrelationInformation)" flag="fatal">CorrelationInformation is not allowed for DOCUMENTID.</assert>
      <assert id="PEPPOL-SBDH-R042" test="not(sbdh:BusinessService)" flag="fatal">BusinessService is not allowed for DOCUMENTID.</assert>
    </rule>
    <rule context="sbdh:BusinessScope/sbdh:Scope[sbdh:Type = 'PROCESSID']">
      <assert id="PEPPOL-SBDH-R050" test="not(sbdh:Identifier)" flag="fatal">Identifier is not allowed for PROCESSID.</assert>
      <assert id="PEPPOL-SBDH-R051" test="not(sbdh:CorrelationInformation)" flag="fatal">CorrelationInformation is not allowed for PROCESSID.</assert>
      <assert id="PEPPOL-SBDH-R052" test="not(sbdh:BusinessService)" flag="fatal">BusinessService is not allowed for PROCESSID.</assert>
    </rule>
    <rule context="sbdh:BusinessScope/sbdh:Scope">
      <assert id="PEPPOL-SBDH-R032" test="index-of(tokenize('DOCUMENTID PROCESSID', '\s'), string(sbdh:Type))" flag="fatal">Only allowed scopes 'DOCUMENTID' and 'PROCESSID' expected.</assert>
    </rule>
  </pattern>
</schema>
