build-ReadFunction:
	echo "Building City Read Function..."
	mvn -T 4C clean native:compile -Pnative -DskipTests -Ddependency-check.skip=true
	cp target/native-image $(ARTIFACTS_DIR)
	cp tools/shell/native/bootstrap $(ARTIFACTS_DIR)
	chmod 755 $(ARTIFACTS_DIR)/bootstrap
