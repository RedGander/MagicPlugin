<?php 

// Magic configuration file path
//$magicRootFolder = '/Users/nathan/Server/plugins/Magic';
$magicRootFolder = '/Users/nathan/Documents/Code/Bukkit/Testing/plugins/Magic';

// Page title
$title = "elMakers Magic Development Site";

// Instructional YouTube video id
$youTubeVideo = 'hzolCW_VLis';

// How players get wands, other than view the configured ways in magic.yml (crafting, random chests)
$howToGetWands = 'You can find wands in chests near spawn';

// Page overview - this will get put in a Header at the top of the page.
$pageOverview = <<<EOT
	<div style="margin-left: 128px;">
		Welcome to the development server for the Magic plugin by elMakers!<br/><br/>
		This is a plugin for the <a href="http://www.bukkit.org" target="_new">Bukkit</a> minecraft server.
		For more information, <a href="http://dev.bukkit.org/bukkit-plugins/magic/" target="_new">click here.</a>
		<br/><br/>
		While this is just a development server, you are free to log in and play at
		<span class="minecraftServer">mine.elmakers.com</span>. You may also view our <a href="http://mine.elmakers.com:8080"/>dynmap here</a>, the world is a bit of a mess.
		<br/><br/>
		Thanks for looking!
	</div>
EOT;

$analytics = <<<EOT
<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-17131761-5', 'elmakers.com');
  ga('send', 'pageview');

</script>
EOT;

?>