from setuptools import setup, find_packages

setup(
    name="helix-wifimic-pc",
    version="1.0.0",
    description="PC client untuk menerima audio stream dari Helix WiFi Mic Android app",
    author="Helix Team",
    packages=find_packages(where="src"),
    package_dir={"": "src"},
    python_requires=">=3.8",
    install_requires=[
        "pyaudio>=0.2.14",
        "numpy>=1.26.0",
    ],
    entry_points={
        "console_scripts": [
            "helix-receiver=receiver.main:main",
        ],
    },
    classifiers=[
        "Development Status :: 3 - Alpha",
        "Intended Audience :: End Users/Desktop",
        "License :: OSI Approved :: MIT License",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.8",
        "Programming Language :: Python :: 3.9",
        "Programming Language :: Python :: 3.10",
        "Programming Language :: Python :: 3.11",
    ],
)
