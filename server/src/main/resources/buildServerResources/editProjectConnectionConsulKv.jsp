<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include-internal.jsp" %>
<%--
  ~ Copyright 2000-2020 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ https://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>
<jsp:useBean id="keys" class="org.jetbrains.teamcity.consul.kv.server.ConsulKvJspKeys"/>

<jsp:useBean id="project" type="jetbrains.buildServer.serverSide.SProject" scope="request"/>
<jsp:useBean id="oauthConnectionBean" type="jetbrains.buildServer.serverSide.oauth.OAuthConnectionBean"
             scope="request"/>
<jsp:useBean id="propertiesBean" type="jetbrains.buildServer.serverSide.oauth.OAuthConnectionBean" scope="request"/>
<bs:linkScript>
    /js/bs/testConnection.js
</bs:linkScript>
<style type="text/css">
    .auth-container {
        display: none;
    }
</style>
<script>
  BS.OAuthConnectionDialog.submitTestConnection = function () {
    var that = this;
    BS.PasswordFormSaver.save(this, '<c:url value="/admin/hashicorp-consul-kv-test-connection.html"/>', OO.extend(BS.ErrorsAwareListener, {
      onFailedTestConnectionError: function (elem) {
        var text = "";
        if (elem.firstChild) {
          text = elem.firstChild.nodeValue;
        }
        BS.TestConnectionDialog.show(false, text, $('testConnectionButton'));
      },

      onCompleteSave: function (form, responseXML) {
        var err = BS.XMLResponse.processErrors(responseXML, this, form.propertiesErrorsHandler);
        BS.ErrorsAwareListener.onCompleteSave(form, responseXML, err);
        if (!err) {
          this.onSuccessfulSave(responseXML);
        }
      },

      onSuccessfulSave: function (responseXML) {
        that.enable();

        var additionalInfo = "";
        var testConnectionResultNodes = responseXML.documentElement.getElementsByTagName("testConnectionResult");
        if (testConnectionResultNodes && testConnectionResultNodes.length > 0) {
          var testConnectionResult = testConnectionResultNodes.item(0);
          if (testConnectionResult.firstChild) {
            additionalInfo = testConnectionResult.firstChild.nodeValue;
          }
        }

        BS.TestConnectionDialog.show(true, additionalInfo, $('testConnectionButton'));
      }
    }));
    return false;
  };

  var afterClose = BS.OAuthConnectionDialog.afterClose;
  BS.OAuthConnectionDialog.afterClose = function () {
    $j('#OAuthConnectionDialog .testConnectionButton').remove();
    afterClose()
  }
</script>
<tr>
    <td><label for="displayName">Display name:</label><l:star/></td>
    <td>
        <props:textProperty name="displayName" className="longField"/>
        <span class="smallNote">Provide some name to distinguish this connection from others.</span>
        <span class="error" id="error_displayName"></span>
    </td>
</tr>
<tr>
    <td><label for="${keys.NAMESPACE}">Parameter namespace:</label></td>
    <td>
        <props:textProperty name="${keys.NAMESPACE}" className="longField"/>
        <span class="error" id="error_${keys.NAMESPACE}"></span>
        <span class="smallNote">Provide some namespace to use in TeamCity parameters in case of multiple Consul connections.</span>
    </td>
</tr>
<tr>
    <td><label for="${keys.URL}">Consul Api URL:</label></td>
    <td>
        <props:textProperty name="${keys.URL}"
                            className="longField textProperty_max-width js_max-width"/>
        <span class="error" id="error_${keys.URL}"/>
        <span class="smallNote">Format: https://&lt;Consulserver&gt;:&lt;port&gt;</span>
    </td>
</tr>


<tr>
    <td><label for="${keys.FAIL_ON_ERROR}">Fail in case of error</label></td>
    <td>
        <props:checkboxProperty name="${keys.FAIL_ON_ERROR}"/>
        <span class="error" id="error_${keys.FAIL_ON_ERROR}"/>
        <span class="smallNote">Whether to fail builds in case of parameter resolving error</span>
    </td>
</tr>

<forms:submit id="testConnectionButton" type="button" label="Test Connection" className="testConnectionButton"
              onclick="return BS.OAuthConnectionDialog.submitTestConnection();"/>
<bs:dialog dialogId="testConnectionDialog" title="Test Connection" closeCommand="BS.TestConnectionDialog.close();"
           closeAttrs="showdiscardchangesmessage='false'">
    <div id="testConnectionStatus"></div>
    <div id="testConnectionDetails" class="mono"></div>
</bs:dialog>
<script>
    $j('#OAuthConnectionDialog .popupSaveButtonsBlock .testConnectionButton').remove();
    $j("#testConnectionButton").appendTo($j('#OAuthConnectionDialog .popupSaveButtonsBlock')[0])
    BS.ConsulKv = {
        onAuthChange: function (element) {
            $j('.auth-container').hide();
            let value = $j(element).val();
            $j('.auth-' + value).show();
            BS.VisibilityHandlers.updateVisibility('mainContent');
        }
    }

   
</script>
