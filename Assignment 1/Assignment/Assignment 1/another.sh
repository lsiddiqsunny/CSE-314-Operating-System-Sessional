

i="Mohammod Riad Ashraf_3000172_assignsubmission_file_1405115.zip"


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
          :
        fi
    else 
        var2=`ls`
        mkdir "$var1"
        for j in $var2
        do

            mv "$j" "$var1"

        done
        var2="7"
        var3="${#var1}"
        if [ "$var2" = "$var3" ]
        then 
            mv "$var1" ../output
            echo "$var1-0">>../output/marks.txt
            echo "$(grep -v $var1 ../output/absent.txt)">../output/absent.txt #removing roll from absent list
        else

                    var2="$(echo $i|cut -d'_' -f1)"
                    var3="$(grep -i "$var2" CSE_322.csv|wc -l)"
                    var5="1"
                    if [ "$var3" = "$var5" ]
                    then 
                        var3="$(grep -i "$var2" CSE_322.csv|cut -d',' -f1|sed -e 's/^"//' -e 's/"$//'|sed -e 's/^[[:space:]]*//')" 
                        mv "$var1" "$var3"
                        mv "$var3" ../output
                        echo "$var3-0">>../output/marks.txt
                        echo "$(grep -v $var3 ../output/absent.txt)">../output/absent.txt #removing roll from absent list


                    else 
                        
                        #echo "$var4">../check5.txt
                        count=$((0))  #counting from zero
                        var2="$(echo $i|cut -d'_' -f1)" #student name
                        var3="$(grep -i "$var2" CSE_322.csv|cut -d',' -f1|sed -e 's/^"//' -e 's/"$//'|sed -e 's/^[[:space:]]*//')" #taking matchedr roll numbe
                        rem=""
                        var5="1"
                        for j in var3
                        do
                           #checking in the absent list
                            var6="$(grep "$j" ../output/absent.txt|wc -l)"
                            if [ "$var5" = "$var6" ]
                            then
                                #increaing instance of id for same name 
                                ((count++))
                                rem="$j" 
                            fi
                            
                        done
                        #find one absent student
                        if [ "$var5" = "$count" ]
                        then
                            mv "$var1" "$rem"
                            mv "$rem" ../output
                            echo "$rem-0">>../output/marks.txt
                            echo "$(grep -v $rem ../output/absent.txt)">../output/absent.txt #removing roll from absent list
                        else
                        #can not identify uniquly,move to extra
                        mv "$var1" "$var2"
                        mv "$var2" ../output/Extra

                        fi    
                    fi

        fi

    fi
    cd ..
 

