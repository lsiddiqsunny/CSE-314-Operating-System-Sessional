#unzip SubmissionAll.zip -d AllSubmission #for unzipping

#ls | cut -d'_' -f1 # to find name

var1="$(ls AllSubmission | cut -d'_' -f1)" 
echo $var1

