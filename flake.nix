{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-parts.url = "github:hercules-ci/flake-parts";
  };

  outputs = {
    self,
    flake-parts,
    ...
  } @ inputs:
    flake-parts.lib.mkFlake {inherit inputs;} {
      systems = ["x86_64-linux"];

      perSystem = {
        config,
        lib,
        pkgs,
        system,
        ...
      }: let
        javaVersion = 21;

        jdk = pkgs."temurin-bin-${toString javaVersion}";
        gradle = pkgs.gradle.override { java = jdk; };

        hsdis = pkgs.stdenv.mkDerivation {
            name = "hsdis";
            src = pkgs.fetchurl {
                url = "https://chriswhocodes.com/hsdis/hsdis-amd64.so";
                hash = "sha256-Lr0Tyg3Qo/IMSbmcErcuN2tsNxl19zRAMEjd89e1FQc=";
            };
            phases = [ "installPhase" ];

            installPhase = ''
              mkdir -p $out/lib
              ln -s $src $out/lib/hsdis-amd64.so
            '';
        };
        profilers = with pkgs; [ async-profiler ] ++ [ hsdis];
       in {
         devShells.default = pkgs.mkShell {
           name = "Jack";
           packages = with pkgs; [git gradle jdk linuxPackages_latest.perf ] ++ profilers;

           shellHook = ''
             export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:${lib.makeLibraryPath profilers}"
             gradle setup
           '';
         };
       };
    };
}