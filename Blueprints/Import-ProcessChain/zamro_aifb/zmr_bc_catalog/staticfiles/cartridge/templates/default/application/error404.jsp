<%-- tell the webadapter not to cache this page --%>
<%  response.setStatus(HttpServletResponse.SC_NOT_FOUND);  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
    <head>
        <title>URL Error.</title>
        <link rel="STYLESHEET" type="text/css" href="/waroot/style.css"/>
    </head>
    <body>
		<div class="sysLabel"></div>
      	<div class="sysImage"></div>
      	<div class="sysMessage">The requested resource is not available!</div>
      	
		<script type="text/javascript">
	          var dataLayer = [{
	                 "version": "1.0.0",
	                 "page": {},
	                 "user": {},
	                 "ecommerce": {}
	          }];
		</script>
		
		<!-- Google Tag Manager -->
		<script type="text/javascript">(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
		    new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
		    j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
		    '//www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
		    })(window,document,'script','dataLayer','GTM-TBD2LH');
		</script>
		
		<script type="text/javascript">
		    dataLayer.push(
		        {
		            'event' : 'pageError',
		            'httpStatus' : 'adapter: 404',
		            'currentUrl': window.location.href
		        }
		    );
		</script>
    </body>
</html>



