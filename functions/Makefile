all: build-CityDataFunction

build-CityDataFunction:
	echo "Building City Data Function..."
	./mvnw -T 4C clean native:compile -Pnative -DskipTests -f ./city-data-function -Ddependency-check.skip=true
	cp ./city-data-function/target/native $(ARTIFACTS_DIR)
	cp ./city-data-function/tools/shell/bootstrap $(ARTIFACTS_DIR)
	chmod 755 $(ARTIFACTS_DIR)/bootstrap
