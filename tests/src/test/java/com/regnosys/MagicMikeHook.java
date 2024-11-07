package com.regnosys;

import cdm.base.staticdata.party.Party;
import com.regnosys.rosetta.generator.object.ExpandedAttribute;
import com.regnosys.rosetta.translate.datamodel.Entity;
import com.regnosys.rosetta.translate.datamodel.NamespaceName;
import com.regnosys.rosetta.translate.synonymmap.*;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MagicMikeHook {

    public void hook1(Object... o) {

        System.out.println();
    }

    public void hook2(Object... o) {
        System.out.println();
// post combine
    }

    public void hook3(Object... o) {
        System.out.println();

    }

    public void hook4(Object... o) {

        SynonymMap tradeStateSynonymMap = ((List<SynonymMap>) o[0]).get(0);

        /*
        Party:
	 	[value "Party" meta "id" maps 2]
        + name
            [value "partyName" meta "entityNameScheme"]
            [value "entityName" meta "entityNameScheme"]
            [value "entityName" path "referenceEntity" meta "entityNameScheme"]
         */

        List<SynonymMap> partyMappings = tradeStateSynonymMap.getForClass(Party.class.getSimpleName());
        for (SynonymMap partyMapping : partyMappings) {
            List<AttributeGroupMapping> mappedGroups = partyMapping.getMappedGroups();
            for (AttributeGroupMapping g : mappedGroups) {
                AttributeGroup fpmlCdmMergedAttributeGroup = g.getGroup();
                List<ExpandedAttribute> cdmPartAttributePath = fpmlCdmMergedAttributeGroup.getAttributePath();

                for (SynonymGroup synonymGroups : fpmlCdmMergedAttributeGroup.getSynonymGroups()) {
                    List<SynonymValue> fpmlPartSynonymVal = synonymGroups.getSynonymValues();
                    for (SynonymValue synonymValue : fpmlPartSynonymVal) {
                        List<Element> synonymPath = synonymValue.getSynonymPath();

                        for (Element element : synonymPath) {

//                            if (element.getName().equals("partyName")) 
                            {
                                System.out.printf("");


                                String rosettaPath = cdmPartAttributePath.stream().map(ExpandedAttribute::getName).collect(Collectors.joining(" -> "));

                                String cdmPartyTypeName = cdmPartAttributePath.get(0).getEnclosingType();

                                String fpmlAttributeName = element.getName();
                                if (element.getEntity() != null) {

                                    String fpmlPartyNameComplexTypeName = element.getEntity().getName().getName();
                                    boolean fpmlHasData = element.getEntity().hasData();

                                    if (fpmlHasData) {
                                        System.out.println("" +
                                                String.format("func Map%sTo%s\n" +
                                                                "  inputs %s %s (0..1)\n" +
                                                                "  output out %s (0..1)\n" +
                                                                "\n" +
                                                                "  set out -> %s: in -> %s\n\n",
                                                        fpmlPartyNameComplexTypeName,
                                                        cdmPartyTypeName,
                                                        fpmlAttributeName,
                                                        fpmlPartyNameComplexTypeName,
                                                        cdmPartyTypeName,
                                                        rosettaPath,
                                                        "value"
                                                ));

                                        System.out.printf("");
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
        System.out.println();

    }

    private static void hack(SynonymMap partyMapping) throws NoSuchFieldException, IllegalAccessException {
        Field attributeGroupMap = SynonymMap.class.getField("attributeGroupMap");
        attributeGroupMap.setAccessible(true);
        Object o1 = attributeGroupMap.get(partyMapping);
    }
}
