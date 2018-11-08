#unzip SubmissionAll.zip -d AllSubmission #for unzipping

#ls | cut -d'_' -f1 # to find name
touch temp.txt
IFS=$(echo -en "\n\b")
mkdir output
touch present.txt 
touch output/absent.txt 


var1="$(ls AllSubmission | cut -d'_' -f5-|cut -d'.' -f1)" #finding all submission name by last field


#all roll number from csv file and remove space and quote and copy to temp.txt
var2="$(cut -d',' -f1 CSE_322.csv|sed -e 's/^"//' -e 's/"$//'|sed -e 's/^[[:space:]]*//')" 
echo "$var2">temp.txt 


for i in $var1
do  
    var3="${#i}"
    var4="7"
    if [ "$var3" = "$var4" ] #roll in zip file match with roster
    then
        echo "$i">>present.txt
    else 
        :
    fi
done

#making absent list, who is not present in list must go to absent list
for i in $var2
do
    
        if grep -qi "$i" present.txt;then
            :
        else
        echo "$i">>output/absent.txt
        fi
    
done

rm present.txt
rm temp.txt
unset IFS

touch output/marks.txt
mkdir output/Extra
touch ../check2.txt
touch ../check3.txt
touch ../check4.txt
touch ../check5.txt
touch ../check6.txt
value1=`ls AllSubmission`

IFS=$(echo -en "\n\b")
for i in $value1
do

    var1="$(echo $i|rev|cut -d'.' -f1|rev)"
    var2="zip"

    #make tempdir
    mkdir tempdir

    #unzip file depend on zip or rar extension
    if [ "$var1" = "$var2" ]
    then unzip "AllSubmission/$i" -d tempdir
    else unrar x "AllSubmission/$i"  tempdir
    fi

    #take the file name with roll number
    var1="$(echo "AllSubmission/$i"|cut -d'_' -f5-|cut -d'.' -f1)"
    cd tempdir


    #count number of files in tempdir
    var2=`ls -1 | wc -l`
    var3="1"

    if [ "$var2" = "$var3" ] #if number of files is one
    then
        var4="$(ls)"
        if [ "$var4" = "$var1" ]
        then
            #match with roll number,get full marks
            echo "$var1 ">>../check2.txt
            mv "$var4" ../output
            echo "$var1-10">>../output/marks.txt
        elif [[ $var4 =~ $var1 ]];
        then
            #contain the roll number,get half marks
            echo "$var1 ">>../check3.txt
            mv "$var4" "$var1"
            mv "$var1" ../output
            echo "$var1-5">>../output/marks.txt

        else 
          
           var2="7"
           var3="${#var1}"
           if [ "$var2" = "$var3" ]
           then 
                #found roll number in the zip file,get zero
                echo "$var1 ">>../check4.txt
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
                        echo "$var4">../check5.txt
                        mv "$var4" "$var2"
                        mv "$var2" ../output/Extra
                        
                    fi
                fi
            fi
        fi
    else 
        echo "$var1 ">>../check6.txt
    fi

    cd ..
    rm -r tempdir
done
unset IFS
