#unzip submissionsAll.zip -d AllSubmission #for unzipping

touch temp.txt
IFS=$(echo -en "\n\b")
mkdir output
touch present.txt 
touch output/absent.txt 


var1="$(ls AllSubmission | cut -d'_' -f5-|cut -d'.' -f1)" #finding all submission name by last field


#all roll number from csv file and remove space and quote and copy to temp.txt
var2="$(cut -d',' -f1 CSE_322.csv|tr -d '"'|tr -d '\t')" 
echo "$var2">temp.txt 


for i in $var1
do  
    if grep -qi "$i" temp.txt;then #roll in zip file match with roster
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

rm temp.txt

touch output/marks.txt
mkdir output/Extra
touch check2.txt
touch check3.txt
touch check4.txt
touch check5.txt
touch check6.txt
value1=`ls AllSubmission`

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
        #var5="7"
        #var6="${#var1}"


        var7="$(grep  "$var1" ../CSE_322.csv|wc -l)"
        var8="1"


        if [[ "$var4" = "$var1" && "$var7" = "$var8" ]]
        then
            #match with roll number,get full marks
            #echo "$var1 ">>../check2.txt
            mv "$var4" ../output
            echo "$var1-10">>../output/marks.txt
        elif [[ $var4 =~ $var1 && "$var7" = "$var8" ]];
        then
            #contain the roll number,get half marks
            echo "$var1 ">>../check3.txt
            mv "$var4" "$var1"
            mv "$var1" ../output
            echo "$var1-5">>../output/marks.txt

        else 

           if [ "$var7" = "$var8" ]
           then 
                #found roll number in the zip file,get zero
                # echo "$var1 ">>../check4.txt
                mv "$var4" "$var1"
                mv "$var1" ../output
                echo "$var1-0">>../output/marks.txt
                echo "$(grep -v $var1 ../output/absent.txt)">../output/absent.txt #removing roll from absent list
           else
                #1.roll number in directory ok but not ok in zip  
                #2.roll number in directory not ok and also in zip
                var7="$(grep  "$var4" ../CSE_322.csv|wc -l)"
                var8="1"
              
                if [ "$var7" = "$var8" ]
                then 
                    
                    mv "$var4" ../output
                    echo "$var4-0">>../output/marks.txt
                    echo "$(grep -v $var4 ../output/absent.txt)">../output/absent.txt #removing roll from absent list

                else
                    var2="$(echo $i|cut -d'_' -f1)"
                    var3="$(grep -i "$var2" ../CSE_322.csv|wc -l)"
                    var5="1"
                    if [ "$var3" = "$var5" ]
                    then 
                        var3="$(grep -i "$var2" ../CSE_322.csv|cut -d',' -f1|tr -d '"'|tr -d '\t')" 
                        mv "$var4" "$var3"
                        mv "$var3" ../output
                        echo "$var3-0">>../output/marks.txt
                        echo "$(grep -v $var3 ../output/absent.txt)">../output/absent.txt #removing roll from absent list


                    else 
                        
                        #echo "$var4">../check5.txt
                        count=$((0))  #counting from zero
                        var2="$(echo $i|cut -d'_' -f1)" #student name
                        var3="$(grep -i "$var2" ../CSE_322.csv|cut -d',' -f1|tr -d '"'|tr -d '\t')" #taking matched roll number
                        rem=""
                        var5="1"
                        for j in $var3
                        do
                           #checking in the absent list
                            var6="$(grep $j ../output/absent.txt|wc -l)"
                            if [ "$var5" = "$var6" ]
                            then
                                #increaing instance of id for same name 
                                ((count++))
                                rem=$j 
                            fi
                            #echo "$j">../check5.txt
                            
                        done
                        #find one absent student
                        
                        if [ "$var5" = "$count" ]
                        then
                            mv "$var4" "$rem"
                            mv "$rem" ../output
                            echo "$rem-0">>../output/marks.txt
                            echo "$(grep -v $rem ../output/absent.txt)">../output/absent.txt #removing roll from absent list
                        else
                        #can not identify uniquly,move to extra
                        mv "$var4" "$var2"
                        mv "$var2" ../output/Extra

                        fi    
                    fi
                fi
            fi
        fi
    else 
    #echo "$var1 ">>../check5.txt
    var2=`ls`
    mkdir "$var1"
        for j in $var2
        do

            mv "$j" "$var1"

        done
        var2="$(grep  "$var1" ../CSE_322.csv|wc -l)"
        var3="1"
        if [ "$var2" = "$var3" ]
        then 
            mv "$var1" ../output
            echo "$var1-0">>../output/marks.txt
            echo "$(grep -v $var1 ../output/absent.txt)">../output/absent.txt #removing roll from absent list
        else

                    var2="$(echo $i|cut -d'_' -f1)"
                    var3="$(grep -i "$var2" ../CSE_322.csv|wc -l)"
                    var5="1"
                    if [ "$var3" = "$var5" ]
                    then 
                        var3="$(grep -i "$var2" ../CSE_322.csv|cut -d',' -f1|tr -d '"'|tr -d '\t')" 
                        mv "$var1" "$var3"
                        mv "$var3" ../output
                        echo "$var3-0">>../output/marks.txt
                        echo "$(grep -v $var3 ../output/absent.txt)">../output/absent.txt #removing roll from absent list


                    else 
                        
                        #echo "$var4">../check5.txt
                        count=$((0))  #counting from zero
                        var2="$(echo $i|cut -d'_' -f1)" #student name
                        var3="$(grep -i "$var2" ../CSE_322.csv|cut -d',' -f1|tr -d '"'|tr -d '\t')" #taking matchedr roll numbe
                        rem=""
                        var5="1"
                        for j in $var3
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
    rm -r tempdir
done
unset IFS
rm check2.txt
rm check3.txt
rm check4.txt
rm check5.txt
rm check6.txt
rm present.txt