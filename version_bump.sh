#!/bin/bash
VERSION='1.1'
NEXT='2.0-SNAPSHOT'
TAG='v1.1'
TAG_NEXT='v2.0'

function bumpSilverWare {
	echo "Bumping from version $VERSION to version $NEXT"

	for f in $(find . -maxdepth 2 -name 'pom.xml'); do 
		echo "Updating file $f..."
		sed -i -e "s/<version>$VERSION<\/version>/<version>$NEXT<\/version>/g" $f
		sed -i -e "s/<tag>$TAG<\/tag>/<tag>$TAG_NEXT<\/tag>/g" $f
	done

	echo "Successfully finished!"
}

echo "Do you wish to bump SilverWare major version from $VERSION to $NEXT?"
select yn in "Yes" "No"; do
    case $yn in
        Yes ) bumpSilverWare; break;;
        No ) echo "Terminating."; exit 1;;
    esac
done
