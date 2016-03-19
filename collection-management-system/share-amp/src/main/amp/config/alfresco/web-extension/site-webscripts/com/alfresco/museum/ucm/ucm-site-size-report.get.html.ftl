<style type="text/css">
   div.report-body {
      text-align: center;
   }

   table, td, th {
      border:1px solid #e5eff8;
   }

   table {
      margin:1em auto;
      border-collapse:collapse;
   }

   td {
      padding:.3em 1em;
      color:#678197;
   }

   thead th {
      padding:.3em 1em;
      background:#f4f9fe;
      color:#66a3d3;
      font:bold 1.2em/2em "Century Gothic","Trebuchet MS",Arial,Helvetica,sans-serif;
   }

   table.report-table thead th {
      background-image: url(data:image/gif;base64,R0lGODlhFQAJAIAAACMtMP///yH5BAEAAAEALAAAAAAVAAkAAAIXjI+AywnaYnhUMoqt3gZXPmVg94yJVQAAOw==);
      background-repeat: no-repeat;
      background-position: center right;
      cursor: pointer;
   }

   table.report-table thead th.sort_column_asc {
      background-image: url(data:image/gif;base64,R0lGODlhFQAEAIAAACMtMP///yH5BAEAAAEALAAAAAAVAAQAAAINjI8Bya2wnINUMopZAQA7);
   }

   table.report-table thead th.sort_column_desc {
      background-image: url(data:image/gif;base64,R0lGODlhFQAEAIAAACMtMP///yH5BAEAAAEALAAAAAAVAAQAAAINjB+gC+jP2ptn0WskLQA7);
   }

   a.likeabutton {
      appearance: button;
      -moz-appearance: button;
      -webkit-appearance: button;
      text-decoration: none; font: menu; color: ButtonText;
      display: inline-block; padding: 2px 8px;
   }
</style>

<@uniqueIdDiv>
   <#assign el=args.htmlid?html>
   <div id="${el}-body" class="report-body">
      <!-- Sortable JQuery TABLE -->
      <table id="${el}-table" class="report-table">
         <thead>
            <tr>
               <th data-type="custom" style="min-width:200px">Path to object</th>
               <th data-type="custom">Size (bytes)</th>
           </tr>
         </thead>
         <tbody>
      <#list data as record>
            <tr><td style="text-align:left">${record[0]}</td><td style="text-align:right">${record[1]}</td></tr>
      </#list>
	     </tbody>
      </table>
      <!-- Making table sortable -->
      <script type="text/javascript">
         require(["jquery"], function($) {
            jQuery = $;
            require(["${url.context}/res/js/jquery.table_sort.min.js"], function() {
               $("#${el}-table").tableSort({
                  compare: function(a, b) {
                     // ['a', 'a/b', 'ab', 'ab/cd', 'ab/cd/e', 'ab/ce']
                     if (a.includes('/') && b.includes('/')) {
                       a = a.split('/');
                       b = b.split('/');
                     }
                     else {
                        // '123,456' -> '123456'
                        if (a.search(new RegExp('^[\\d,]+$')) != -1 && b.search(new RegExp('^[\\d,]+$')) != -1) {
                           a = parseInt(a.split(',').join(''));
                           b = parseInt(b.split(',').join(''));
                        }
                     }
                     if (a > b) return 1;
                     if (a < b) return -1;
                     return 0;
                  }
               });
            });
         });
      </script>

      <!-- Link to report in XLS form -->
      <a href="/alfresco/service/ucm/site-size-report-xls?nodeRef=${siteRef}" class="likeabutton" download target="_blank">Save report</a>
   </div>
</@>
