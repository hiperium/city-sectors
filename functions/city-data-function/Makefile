build-CityDataFunction:
	echo "Building City Data Function..."
	mvn -T 4C clean native:compile -Pnative -DskipTests -Ddependency-check.skip=true
	cp target/native $(ARTIFACTS_DIR)
	cp tools/shell/bootstrap $(ARTIFACTS_DIR)
	chmod 755 $(ARTIFACTS_DIR)/bootstrap
