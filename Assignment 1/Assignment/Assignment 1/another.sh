

i="Zaara Tasnim_3000810_assignsubmission_file_1405019_assignment1.zip"


    var1="$(echo $i|rev|cut -d'.' -f1|rev)"
    var2="zip"
    if [ "$var1" = "$var2" ]
    then unzip "AllSubmission/$i" -d tempdir
    else unrar x "AllSubmission/$i"  tempdir
    fi

    var1="$(echo "AllSubmission/$i"|cut -d'_' -f5-|cut -d'.' -f1)"
    cd tempdir
    var2=`ls -1 | wc -l`
    var3="1"

    if [ "$var2" = "$var3" ]
    then
        var4="$(ls)"
        if [ "$var4" = "$var1" ]
        then

         
            :
        elif [[ $var4 =~ $var1 ]];
        then
          :
           

        else 
           var2="7"
           var3="${#var1}"
           if [ "$var2" = "$var3 " ]
           then 
                #found roll number in the zip file,get zero
                mv "$var4" "$var1"
                mv "$var1" ../output
                echo "$var1-0">>../output/marks.txt
                echo "$(grep -v $var1 ../output/absent.txt)">../output/absent.txt #removing roll from absent list
           else
                #1.roll number in directory ok but not ok in zip  
                #2.roll number in directory not ok and also in zip
                var2="7"
                var3="${#var4}"
              
                if [ "$var2" = "$var3" ]
                then 
                    
                    mv "$var4" ../output
                    echo "$var4-0">>../output/marks.txt
                    echo "$(grep -v $var4 ../output/absent.txt)">../output/absent.txt #removing roll from absent list

                else
                    var2="$(echo $i|cut -d'_' -f1)"
                    var3="$(grep -i $var2 CSE_322.csv|wc -l)"
                    var5="1"
                    if [ "$var3" = "$var5" ]
                    then 
                        var3="$(grep -i "$var2" CSE_322.csv|cut -d',' -f1|sed -e 's/^"//' -e 's/"$//'|sed -e 's/^[[:space:]]*//')" 
                        mv "$var4" "$var3"
                        mv "$var3" ../output
                        echo "$var3-0">>../output/marks.txt
                        echo "$(grep -v $var3 ../output/absent.txt)">../output/absent.txt #removing roll from absent list


                    else 
                        #can not identify,move to extra
                        mv "$var4" "$var2"
                        mv "$var2" ../output/extra
                        
                    fi
                fi
            fi
        fi
    else 
        : 
    fi

    cd ..
 

