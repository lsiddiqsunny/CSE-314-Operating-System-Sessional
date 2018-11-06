#unzip SubmissionAll.zip -d AllSubmission #for unzipping

#ls | cut -d'_' -f1 # to find name
touch temp.txt
touch temp2.txt
touch temp3.txt

var1="$(ls AllSubmission | cut -d'_' -f1)"
var2="$(cut -d',' -f2 CSE_322.csv)"
echo "$var1">temp.txt
echo "$var2">temp2.txt
IFS=$(echo -en "\n\b")
touch present.txt
touch absent.txt
for i in $var2
do
    if grep -qi "$i" temp.txt;then
        echo "$(grep $i CSE_322.csv|cut -d',' -f1|sed -e 's/^"//' -e 's/"$//'|sed -e 's/^[[:space:]]*//')">>present.txt
    fi
   
    
done

var3="$(ls AllSubmission | cut -d'_' -f5|cut -d'.' -f1)"

var4="$(cut -d',' -f1 CSE_322.csv|sed -e 's/^"//' -e 's/"$//'|sed -e 's/^[[:space:]]*//')"
echo "$var4">temp3.txt
for i in $var3
do
    
    if grep -qi "$i" temp3.txt;then
        if grep -qi "$i" present.txt;then
            :
        else
        echo "$i">>present.txt
        fi
    fi
done
for i in $var4
do
    
        if grep -qi "$i" present.txt;then
            :
        else
        echo "$i">>absent.txt
        fi
    
done


rm temp.txt
rm temp2.txt
rm temp3.txt
unset IFS


