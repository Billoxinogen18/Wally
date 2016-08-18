(function ($) {

    $.fn.cycle = function (selector) {
        console.log("bla");
        var $container = $(this);

        function cycleImages(){
            var $active = $container.find('.active') || $container.find(selector);
            var $next = ($active.next(selector).length > 0) ? $active.next(selector) : $container.find(selector + ':first');
            $next.css('z-index',2);//move the next image up the pile
            $active.fadeOut(1500,function(){//fade out the top image
                $active.css('z-index',1).show().removeClass('active');//reset the z-index and unhide the image
                $next.css('z-index',3).addClass('active');//make the next image the top one
            });
        }

        setInterval(cycleImages, 2000);

        return this;
    };
})(jQuery);