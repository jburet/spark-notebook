angular.module('sparkNotebook.directives', [])
.directive('displayData', function() {
  return {
    restrict: 'A',
    link: function (scope, element, attrs) {
      var data = scope[attrs.data].data
      // check type for determining type of viz
      switch (data.type) {
        case "table":
          var table = document.createElement("data-table")
          table.setAttribute("data", JSON.stringify(data.data))
          element[0].appendChild(table)
          break;
        case "list":
          var table = document.createElement("data-list")
          table.setAttribute("data", JSON.stringify(data.data))
          element[0].appendChild(table)
          break;
        case "histogram":
          console.log(data)
          break;
        case "distribution":
          var table = document.createElement("graph-distribution")
          table.setAttribute("data", JSON.stringify(data.data))
          table.setAttribute("container", 'graph')
          element[0].appendChild(table)
          var container = document.createElement("div")
          container.setAttribute("id", "graph");
          element[0].appendChild(container)
          break;
        case "heatmap":
          var table = document.createElement("graph-heatmap")
          table.setAttribute("data", JSON.stringify(data.data))
          table.setAttribute("container", 'graph')
          element[0].appendChild(table)
          var container = document.createElement("div")
          container.setAttribute("id", "graph");
          element[0].appendChild(container)
          break;
        case "scatter":
          var table = document.createElement("graph-scatter")
          table.setAttribute("data", JSON.stringify(data.data))
          table.setAttribute("container", 'graph')
          element[0].appendChild(table)
          var container = document.createElement("div")
          container.setAttribute("id", "graph");
          element[0].appendChild(container)
          break;
      }
    }
  };
});
