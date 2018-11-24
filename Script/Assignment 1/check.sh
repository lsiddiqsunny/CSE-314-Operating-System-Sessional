

unrar x "AllSubmission/Zahid hassan_2997010_assignsubmission_file_1405064.rar"  tempdir


var1="$(echo "AllSubmission/Zahid hassan_2997010_assignsubmission_file_1405064.rar"|cut -d'_' -f5-|cut -d'.' -f1)"
cd tempdir



var2=`ls -1 | wc -l`
echo "$var2"
var3="1"

if [ "$var2" = "$var3" ]
then
    var4="$(ls)"
    if [ "$var4" = "$var1" ]
    then
        mv "$var4" ../output
        echo "$var1-10">>../output/marks.txt
    elif [ $var4 =~ $var1 ]
    then
        mv "$var4" "$var1"
        mv "$var1" ../output
        echo "$var1-5">>../output/marks.txt

    else 
        var1="$(echo "AllSubmission/Zahid hassan_2997010_assignsubmission_file_1405064.rar"|cut -d'/' -f2-|cut -d'_' -f1)"
        


    fi

else 
    echo "here"
fi

cd ..