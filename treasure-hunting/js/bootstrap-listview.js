(function ($) {
    function getSeparator(title) {
        return $("<li>").addClass("separator").html(title);
    }

    $.fn.bslistview = function (adapter, onSelectCallback) {
        var listView = {
            listData: {},
            views: {},
            orderedData: [],
            layoutManager: {
                getElement: function() { return $("<li>").addClass("element").addClass("list-group-item") }
            },
            ul: null
        };

        listView.ul = $(this);
        listView.adapter = adapter;
        listView.onSelectCallback = onSelectCallback;

        listView.prepend = function (id, data) {
            if(listView.getCount() == 0){
                listView.ul.empty();
            }


            var $li = listView.layoutManager.getElement().data("id", id);

            var html = $(listView.adapter.getHtml(data));

            html.appendTo($li.find(".element").addBack('.element'));

            $li.click(function(e){
                // var id = $(e.delegateTarget).data('id');
                var elem = listView.listData[id];
                elem.id = id;
                listView.onSelectCallback(listView.listData[id], e);
            });

            if(adapter.separator) {
                if (listView.orderedData.length != 0) {
                    var separator = adapter.separator(data, listView.orderedData[0]);
                    if (separator) {
                        listView.ul.prepend(getSeparator(separator));
                    }
                }
            }

            $li.hide();
            listView.ul.prepend($li);
            $li.slideDown();

            listView.listData[id] = data;
            listView.views[id] = $li;
            listView.orderedData.unshift(data);

            return html;
        };

        listView.remove = function (id) {
            listView.ul.find('li').each(function(index, elem){
                if ($(elem).data("id") == id) {
                    $(elem).fadeOut("fast", function() {
                        $(elem).remove();
                    });
                }
            });
            var index = listView.orderedData.indexOf(id);
            listView.orderedData.splice(index, 1);
            delete listView.listData[id];
        };

        listView.get = function (id){
            return listView.listData[id];
        };

        listView.update = function(id, data){
            listView.listData[id] = data;
            var $li = listView.views[id];

            var html = $(listView.adapter.getHtml(data));
            var $element = $li.find(".element").addBack('.element').html("");
            html.appendTo($element);

        };

        listView.getCount = function () {
            return Object.keys(listView.listData).length;
        };

        listView.select = function(id){
            if(listView.views[id])
                listView.views[id].click();
        };

        listView.setLayoutManager = function (layoutManager) {
            listView.layoutManager = layoutManager;
        };

        listView.items = function(){
          return listView.listData;
        };

        return listView;
    };
})(jQuery);

function createLayoutManager(html){
    return {
        getElement: function() { return $(html) }
    }
}


function createAdapter(getHtml, separator) {
    return {
        getHtml: getHtml,
        separator: separator
    }
}