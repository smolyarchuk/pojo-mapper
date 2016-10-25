# Simple Pojo Mapper

Simple Pojo Mapper is a library that recursively copies data from one object to another.
It based on Apache commons-beanutils and provides flexible API for its.
It supports multiple sources and bi-directional mapping, customizable and has some nice defaults.

## Installation

In a development environment or console, use **mvn install** to build it from sources and install artifact into the local repository.

### Sample of Usage
...
SomeTargetPojo result = PojoMapper.copyTo(new SomeTargetPojo()).from(source).copy();

...
SomeTargetPojo result = PojoMapper.copyTo(new SomeTargetPojo())
	.from(firstSource)
		.ignore("ignorableProperty")
	.from(secondSource)
		.mapper("prop1", "prop2")
		.rewrite(true)
		.skipNulls(true)
	.copy();

...see unit tests for more examples 

## Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D

## Credits

Mail - smolyarchyk@gmail.com
Skype - sergey.smolyarchuk