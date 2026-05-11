import { useContext } from "react";
import { NucleusContext } from "../provider/NucleusProvider";

export function useNucleus() {
  const value = useContext(NucleusContext);
  if (!value) {
    throw new Error("useNucleus must be used inside NucleusProvider");
  }
  return value;
}
