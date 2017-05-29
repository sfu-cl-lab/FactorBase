<?php
ob_start();
        function get_hit($counter_file)   
        {
            $count=0;                           

            if(file_exists($counter_file))      
            {
                $fp=fopen($counter_file,"r"); 
                $count=0+fgets($fp,20);
                fclose($fp);                    
            }

            $count++;                         
            $fp=fopen($counter_file,"w");       
            fputs($fp,$count);               
            fclose($fp);
            return($count);                  
        }
?>

<?php
                $hit=get_hit("dlcounter.txt");
                header('Location: http://www.cs.sfu.ca/~oschulte/BayesBase/RunBayesBase1.0.tar.gz');
ob_flush();
?>
