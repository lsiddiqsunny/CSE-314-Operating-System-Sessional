value1=`cat present.txt`
value2=`ls AllSubmission`
touch temp4.txt
echo "$value2">temp4.txt

for i in $value1
do
    var1=`grep $i temp4.txt`
    var1="AllSubmission/$var1"
    echo "$var1"
    unzip "$var1" -d "Final Output Folder Format"
    
    
    
done
