touch output/marks.txt
touch ../check2.txt
touch ../check3.txt
touch ../check4.txt
touch ../check5.txt
value1=`ls AllSubmission`

IFS=$(echo -en "\n\b")
for i in $value1
do

    var1="$(echo $i|rev|cut -d'.' -f1|rev)"
    var2="zip"
    mkdir tempdir
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

            echo "$var1 ">>../check2.txt
            mv "$var4" ../output
            echo "$var1-10">>../output/marks.txt
        elif [[ $var4 =~ $var1 ]];
        then
            echo "$var1 ">>../check3.txt
            mv "$var4" "$var1"
            mv "$var1" ../output
            echo "$var1-5">>../output/marks.txt

        else 
            echo "$var1 ">>../check4.txt
        fi
    else 
        echo "$var1 ">>../check5.txt
    fi

    cd ..
    rm -r tempdir
done
unset IFS