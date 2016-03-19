<!DOCTYPE html>
<html>
<head><meta http-equiv="Content-Type" content="text/html; charset=utf-8"></head>
<body>
<table>
<thead>
	<tr><th>Path to object</th><th>Size (bytes)</th></tr>
</thead>
<tbody>
<#list data as record>
	<tr><td>${record[0]}</td><td>${record[1]}</td></tr>
</#list>
</tbody>
</table>
</body>
</html>