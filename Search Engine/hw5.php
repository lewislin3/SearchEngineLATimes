<?php
// make sure browsers see this page as utf-8 encoded HTML


echo "<body style='background-color:Pink'>";
header('Content-Type: text/html; charset=utf-8');
include '/var/www/html/SpellCorrector.php';
ini_set('max_execution_time', 300);
ini_set('memory_limit','-1');
$limit = 10;
$query = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;
$results = false;
$sortMethod = "Lucene";
$correctedQuery = "";
$displayCorrectMsg = false;
$correctMsg = "";

if ($query)
{
        
    // The Apache Solr Client library should be on the include path
    // which is usually most easily accomplished by placing in the
    // same directory as this script ( . or current directory is a default
    // php include path entry in the php.ini)
    require_once('solr-php-client-master/Apache/Solr/Service.php');
    // create a new solr service instance - host, port, and corename
    // path (all defaults in this example)
    $solr = new Apache_Solr_Service('localhost', 8983, '/solr/csci572/');
    // if magic quotes is enabled then stripslashes will be needed
    
    if(isset($_REQUEST['sort']))$sortMethod =  $_REQUEST['sort'];

    if (get_magic_quotes_gpc() == 1)
    {
        $query = stripslashes($query);
    }
    // in production code you'll always want to use a try /catch for any
    // possible exceptions emitted by searching (i.e. connection
    // problems or a query parsing error)
    try
    {
        $sepWord = explode(" ", $query);
        for($i=0; $i<sizeOf($sepWord); $i++)
        {
            $tmp= SpellCorrector::correct($sepWord[$i]); // slow
            //$tmp = $sepWord[$i];
            $correctedQuery = $correctedQuery." ".trim($tmp);
        }
        $correctedQuery = trim($correctedQuery);
        if(strtolower($query) == strtolower($correctedQuery))
        {
            if($sortMethod != "Lucene")
            {
                $Param=array('sort' => 'pageRankFile desc');
                $results = $solr->search($query, 0, $limit, $Param);
            }
            else
            {
                $results = $solr->search($query, 0, $limit);
            }
        }
        else {
            if($sortMethod != "Lucene")
            {
                $Param=array('sort' => 'pageRankFile desc');
                $results = $solr->search($query, 0, $limit, $Param);
                $displayCorrectMsg = true;
                $Plus = str_replace(" ", "+", $correctedQuery);
                $correctMsg = "<div class='message'>Did you mean: <a href='http://localhost/hw5.php?q=".$Plus."&sort=nx'>".$correctedQuery."</a></div></br>";
            }
            else
            {
                $results = $solr->search($query, 0, $limit);
                $displayCorrectMsg = true;
                $Plus = str_replace(" ", "+", $correctedQuery);
                $correctMsg = "<div class='message'>Did you mean:  <a href='http://localhost/hw5.php?q=".$Plus."&sort=Lucene'>".$correctedQuery."</a></div></br>";
            }
            
        }
        
        
    }
    catch (Exception $e)
    {
    // in production you'd probably log or email this error to an admin
    // and then show a special message to the user but for this example
    // we're going to show the full exception
    die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
    }
}
?>
<html>
    <head>
        <title>Yu-Hsin Lin Assignment 5 PHP</title>
        <link rel="stylesheet" href="http://code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
        <script src="https://code.jquery.com/jquery-3.3.1.min.js"></script>
        <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.min.js"></script>
    </head>
    <body>
        <form accept-charset="utf-8" method="get">
            <h1>Yu-Hsin Lin Assignment 5</h1>
            </br>
            <label for="q">Search:</label>
            <input id="q" name="q" type="text" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>"/>
            <input type="submit"/>
            </br>
            <input type="radio" name="sort" value="nx" <?php if(isset($_REQUEST['sort']) && $sortMethod == "nx") { echo "checked";} ?>>nx
            <input type="radio" name="sort" value="Lucene" <?php if(!isset($_REQUEST['sort']) || $sortMethod == "Lucene") { echo "checked";} ?>>Lucene
        </form>


        <script>
        $(function() {
 
            $("#q").autocomplete({
                source: function(request, response) {
                    var query = $("#q").val().toLowerCase();
                    var prevWord = ""; 
                    var lastWord = query;
                    var space = query.lastIndexOf(' ');
                    if(space >= 0 && space < query.length - 1) {
                        lastWord = query.substr(space + 1);
                        prevWord = query.substr(0, space);
                    }
                    $.ajax({
                        url: "http://localhost:8983/solr/csci572/suggest?q=" + lastWord + "&wt=json",
                        dataType: 'jsonp',
                        jsonp: 'json.wrf',
                        type:'GET',
                        success: function(data) {
                            var result = data.suggest.suggest[lastWord].suggestions;
                            var terms = [];
                            for(var i = 0; i < result.length && i < 5; i++) {
            					var candidate = prevWord + " " + result[i].term;;
                                terms.push(candidate.trim());
                            }
                            response(terms);
                        },
                        error: function(ex) {
                            alert("Error doing autocomplete.");
                        }
                    });
                }, 
                minLength: 1
            }); 
        }); 
    </script>


<?php
// display results

$urlMap =  array_map('str_getcsv', file('URLtoHTML_latimes_news.csv'));
if($displayCorrectMsg)
{
    echo $correctMsg ;
    $displayCorrectMsg = false;
}
if ($results)
{
    
    $total = (int) $results->response->numFound;
    $start = min(1, $total);
    $end = min($limit, $total);
    echo "<div>Results $start - $end of $total </div><ol>";
    
    // iterate result documents
    foreach ($results->response->docs as $doc)
    {
    // iterate document fields / values
    
    $nowTitle = "";
    $nowUrl = "";
    $nowId = "";
    $nowDes = "N/A";
        foreach ($doc as $field => $value)
        {

                if($field == "id")
                {
                    $nowId = $value;
                }
                if($field == "title")
                {
                    $nowTitle = $value;
                }
                if($field == "og_description")
                {
                    $nowDes = $value;
                    if($value == "")$nowDes = "N/A";
                }
                if($field == "og_url")
                {
                    $nowUrl = $value;
                }
        }
        if($nowUrl == "")
        {
            $curId = str_replace("/home/lewis/latimes/latimes/","",$nowId);
            foreach($urlMap as $newRow){
                if($curId == $newRow[0])
                {
                    $nowUrl = $newRow[1];
                }
            }
        }
        echo " <li> 
	        Title: <a href= '$nowUrl'> $nowTitle </a></br>
	        URL: <a href= '$nowUrl'> $nowUrl</a></br>
	        Description: $nowDes</br>
            ID: $nowId </br>
            </br>
	        </li>";
    }
    echo "</ol>";
    
}
?>
    </body>
</html>