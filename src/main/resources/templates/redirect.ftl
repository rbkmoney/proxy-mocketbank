<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body onload=OnLoadEvent();>
<form action="${acsUrl}" METHOD="post" target="_self">
    <input name="cres" type="hidden" value="${cres}">
    <input name="TermUrl" type="hidden" value="${TermUrl}">
    <script>function OnLoadEvent() {document.forms[0].submit();}</script>
</form>
</body>
</html>