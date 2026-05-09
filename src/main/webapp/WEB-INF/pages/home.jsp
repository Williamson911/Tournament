<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*" %>

<html>
<head>
    <title>API Explorer</title>

    <style>

        body {
            font-family: Arial, sans-serif;
            background: #f7f7f7;
            padding: 24px;
        }

        .group {
            margin-bottom: 28px;
        }

        .group-title {
            gap: 0.3em;
            display: flex;
            font-size: 32px;
            font-weight: 700;
            color: #2d3748;
            margin-bottom: 12px;
        }

        .toggle-btn {
            width: 3em;
            border: none;
            height: 3em;
            border-radius: 50%;
        }

        .tags {
            position: relative;
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            min-height: 5em;
        }

        .endpoint {
            position: absolute;
            display: flex;
            align-items: center;

            gap: 10px;

            padding: 15px;

            border-radius: 10px;

            background: white;

            border: 1px solid #ddd;

            text-decoration: none;

            transition: .15s;
        }

        .tags.show .endpoint {
            position: relative;
            left: 0 !important;
            top: 0 !important;
            filter: blur(0px) !important;
            opacity: 1;
        }

        .endpoint:hover {

            transform: translateY(-1px);

            box-shadow: 0 2px 8px rgba(0, 0, 0, .08);
        }

        .method {

            padding: 6px;

            border-radius: 5px;

            color: white;

            font-size: 12px;
            font-weight: bold;
        }

        .GET {
            background: #61affe;
        }

        .POST {
            background: #49cc90;
        }

        .PUT {
            background: #fca130;
        }

        .DELETE {
            background: #f93e3e;
        }

        .summary {

            color: #444;

            font-size: 14px;
        }

        .path {

            color: #999;

            font-family: monospace;

            font-size: 12px;
        }

    </style>
</head>

<body>

<h1>API Explorer</h1>

<%

    Map<String, List<Map<String, String>>> grouped =
            (Map<String, List<Map<String, String>>>) request.getAttribute("grouped");

    for (String group : grouped.keySet()) {

%>

<div class="group">

    <div class="group-title">
        <%= group %>
        <%
            List<Map<String, String>> endpoints = grouped.get(group);
            final int T = endpoints.size();

            if (T > 1) {
        %>
        <button class="toggle-btn"
                onclick="toggleTags(this)">+
        </button>
        <% } %>
    </div>

    <div class="tags" onclick="toggleShow(this)">

        <%


            int c = 0;
            for (int i = T - 1; i >= 0; i--) {

                Map<String, String> ep = endpoints.get(i);

                String method = ep.get("method");
                String path = ep.get("path");
                String summary = ep.get("summary");
                String style = "left:" + (3 * c + "em; top:" + c + "em; opacity:" + (T / (c * 1.0)) + 0.05 + "; filter: blur(" + (T - 1 - c++) + "px)");
        %>

        <a class="endpoint"
           style="<%=style%>"
           href="<%= request.getContextPath() %>/swagger-ui/index.html#/default<%= path %>">

            <div class="method <%= method %>">
                <%= method %>
            </div>

            <div class="summary">
                <%= summary %>
            </div>

            <div class="path">
                <%= path %>
            </div>

        </a>

        <% } %>

    </div>

</div>

<% } %>

</body>
<script>

    function toggleTags(button) {

        const group = button.closest(".group");

        const tags = group.querySelector(".tags");

        tags.classList.toggle("show");

        button.textContent =
            tags.classList.contains("show")
                ? "-"
                : "+";
    }

</script>
</html>